import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { supabase, type Person } from '../lib/supabase';

export default function AddEditPersonPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [gender, setGender] = useState<'MALE' | 'FEMALE' | 'OTHER' | ''>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // For adding relationships
  const [allPersons, setAllPersons] = useState<Person[]>([]);
  const [selectedParents, setSelectedParents] = useState<string[]>([]);
  const [selectedChildren, setSelectedChildren] = useState<string[]>([]);
  const [currentRelationships, setCurrentRelationships] = useState<{
    parents: string[];
    children: string[];
  }>({ parents: [], children: [] });

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchAllPersons();
    if (isEdit) {
      fetchPerson();
    }
  }, [user, id, navigate, isEdit]);

  const fetchAllPersons = async () => {
    try {
      const { data, error } = await supabase
        .from('persons')
        .select('*')
        .eq('user_id', user?.id)
        .order('first_name', { ascending: true });

      if (error) throw error;
      if (data) setAllPersons(data);
    } catch (err: any) {
      console.error('Failed to fetch persons:', err);
    }
  };

  const fetchPerson = async () => {
    try {
      setLoading(true);
      const { data, error } = await supabase
        .from('persons')
        .select('*')
        .eq('id', id)
        .single();

      if (error) throw error;

      if (data) {
        setFirstName(data.first_name);
        setLastName(data.last_name);
        setBirthDate(data.birth_date || '');
        setGender(data.gender || '');

        // Fetch current relationships
        const { data: relationships, error: relError } = await supabase
          .from('relationships')
          .select('*')
          .or(`parent_id.eq.${id},child_id.eq.${id}`);

        if (relError) throw relError;

        const parents = relationships
          ?.filter(rel => rel.child_id === id)
          .map(rel => rel.parent_id) || [];
        
        const children = relationships
          ?.filter(rel => rel.parent_id === id)
          .map(rel => rel.child_id) || [];

        setSelectedParents(parents);
        setSelectedChildren(children);
        setCurrentRelationships({ parents, children });
      }
    } catch (err: any) {
      setError(err.message || 'Failed to fetch person');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      let personId = id;

      if (isEdit) {
        // Update existing person
        const { error } = await supabase
          .from('persons')
          .update({
            first_name: firstName,
            last_name: lastName,
            birth_date: birthDate || null,
            gender: gender || null,
          })
          .eq('id', id);

        if (error) throw error;
      } else {
        // Create new person
        const { data, error } = await supabase
          .from('persons')
          .insert({
            first_name: firstName,
            last_name: lastName,
            birth_date: birthDate || null,
            gender: gender || null,
            user_id: user?.id,
          })
          .select()
          .single();

        if (error) throw error;
        personId = data.id;
      }

      // Handle relationships
      if (personId) {
        // Remove old relationships
        if (isEdit) {
          const parentsToRemove = currentRelationships.parents.filter(
            p => !selectedParents.includes(p)
          );
          const childrenToRemove = currentRelationships.children.filter(
            c => !selectedChildren.includes(c)
          );

          for (const parentId of parentsToRemove) {
            await supabase
              .from('relationships')
              .delete()
              .eq('parent_id', parentId)
              .eq('child_id', personId);
          }

          for (const childId of childrenToRemove) {
            await supabase
              .from('relationships')
              .delete()
              .eq('parent_id', personId)
              .eq('child_id', childId);
          }
        }

        // Add new relationships
        const parentsToAdd = selectedParents.filter(
          p => !currentRelationships.parents.includes(p)
        );
        const childrenToAdd = selectedChildren.filter(
          c => !currentRelationships.children.includes(c)
        );

        for (const parentId of parentsToAdd) {
          await supabase
            .from('relationships')
            .insert({
              parent_id: parentId,
              child_id: personId,
              user_id: user?.id,
            });
        }

        for (const childId of childrenToAdd) {
          await supabase
            .from('relationships')
            .insert({
              parent_id: personId,
              child_id: childId,
              user_id: user?.id,
            });
        }
      }

      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to save person');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this person?')) {
      return;
    }

    try {
      setLoading(true);
      
      // Delete relationships first
      await supabase
        .from('relationships')
        .delete()
        .or(`parent_id.eq.${id},child_id.eq.${id}`);

      // Delete person
      const { error } = await supabase
        .from('persons')
        .delete()
        .eq('id', id);

      if (error) throw error;
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to delete person');
    } finally {
      setLoading(false);
    }
  };

  const toggleParent = (parentId: string) => {
    setSelectedParents(prev =>
      prev.includes(parentId)
        ? prev.filter(p => p !== parentId)
        : [...prev, parentId]
    );
  };

  const toggleChild = (childId: string) => {
    setSelectedChildren(prev =>
      prev.includes(childId)
        ? prev.filter(c => c !== childId)
        : [...prev, childId]
    );
  };

  const availablePersons = allPersons.filter(p => p.id !== id);

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      {/* Header */}
      <header style={{
        backgroundColor: 'white',
        padding: '1rem 2rem',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <h1 style={{ margin: 0 }}>
          {isEdit ? 'Edit Person' : 'Add Person'}
        </h1>
        <button
          onClick={() => navigate('/dashboard')}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Back to Dashboard
        </button>
      </header>

      <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
        {error && (
          <div style={{
            backgroundColor: '#fee',
            color: '#c00',
            padding: '1rem',
            borderRadius: '4px',
            marginBottom: '1rem'
          }}>
            {error}
          </div>
        )}

        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '8px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
        }}>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                First Name *
              </label>
              <input
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                Last Name *
              </label>
              <input
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                Birth Date
              </label>
              <input
                type="date"
                value={birthDate}
                onChange={(e) => setBirthDate(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              />
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                Gender
              </label>
              <select
                value={gender}
                onChange={(e) => setGender(e.target.value as any)}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              >
                <option value="">Select Gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            {/* Parents Section */}
            {availablePersons.length > 0 && (
              <>
                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Parents
                  </label>
                  <div style={{
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    padding: '0.5rem',
                    maxHeight: '150px',
                    overflowY: 'auto'
                  }}>
                    {availablePersons.map(person => (
                      <label
                        key={person.id}
                        style={{
                          display: 'block',
                          padding: '0.5rem',
                          cursor: 'pointer',
                          backgroundColor: selectedParents.includes(person.id) ? '#e3f2fd' : 'transparent'
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={selectedParents.includes(person.id)}
                          onChange={() => toggleParent(person.id)}
                          style={{ marginRight: '0.5rem' }}
                        />
                        {person.first_name} {person.last_name}
                      </label>
                    ))}
                  </div>
                </div>

                {/* Children Section */}
                <div style={{ marginBottom: '1.5rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Children
                  </label>
                  <div style={{
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    padding: '0.5rem',
                    maxHeight: '150px',
                    overflowY: 'auto'
                  }}>
                    {availablePersons.map(person => (
                      <label
                        key={person.id}
                        style={{
                          display: 'block',
                          padding: '0.5rem',
                          cursor: 'pointer',
                          backgroundColor: selectedChildren.includes(person.id) ? '#e3f2fd' : 'transparent'
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={selectedChildren.includes(person.id)}
                          onChange={() => toggleChild(person.id)}
                          style={{ marginRight: '0.5rem' }}
                        />
                        {person.first_name} {person.last_name}
                      </label>
                    ))}
                  </div>
                </div>
              </>
            )}

            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                type="submit"
                disabled={loading}
                style={{
                  flex: 1,
                  padding: '0.75rem',
                  backgroundColor: loading ? '#ccc' : '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '1rem',
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                {loading ? 'Saving...' : (isEdit ? 'Update Person' : 'Create Person')}
              </button>

              {isEdit && (
                <button
                  type="button"
                  onClick={handleDelete}
                  disabled={loading}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: loading ? '#ccc' : '#dc3545',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    fontSize: '1rem',
                    cursor: loading ? 'not-allowed' : 'pointer'
                  }}
                >
                  Delete
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
