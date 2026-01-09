import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { supabase, type Person } from '../lib/supabase';

interface PersonWithRelations extends Person {
  parents?: Person[];
  children?: Person[];
}

export default function DashboardPage() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const [persons, setPersons] = useState<PersonWithRelations[]>([]);
  const [selectedPerson, setSelectedPerson] = useState<PersonWithRelations | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchPersons();
  }, [user, navigate]);

  const fetchPersons = async () => {
    try {
      setLoading(true);
      const { data: personsData, error: personsError } = await supabase
        .from('persons')
        .select('*')
        .eq('user_id', user?.id)
        .order('created_at', { ascending: false });

      if (personsError) throw personsError;

      if (personsData) {
        // Fetch relationships
        const { data: relationshipsData, error: relationshipsError } = await supabase
          .from('relationships')
          .select('*')
          .eq('user_id', user?.id);

        if (relationshipsError) throw relationshipsError;

        // Map persons with their relationships
        const personsWithRelations: PersonWithRelations[] = personsData.map(person => {
          const parents = relationshipsData
            ?.filter(rel => rel.child_id === person.id)
            .map(rel => personsData.find(p => p.id === rel.parent_id))
            .filter(Boolean) as Person[] || [];

          const children = relationshipsData
            ?.filter(rel => rel.parent_id === person.id)
            .map(rel => personsData.find(p => p.id === rel.child_id))
            .filter(Boolean) as Person[] || [];

          return { ...person, parents, children };
        });

        setPersons(personsWithRelations);
        if (personsWithRelations.length > 0 && !selectedPerson) {
          setSelectedPerson(personsWithRelations[0]);
        }
      }
    } catch (err: any) {
      setError(err.message || 'Failed to fetch persons');
    } finally {
      setLoading(false);
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut();
      navigate('/login');
    } catch (err: any) {
      setError(err.message || 'Failed to sign out');
    }
  };

  const selectPerson = (person: PersonWithRelations) => {
    setSelectedPerson(person);
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        Loading...
      </div>
    );
  }

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
        <h1 style={{ margin: 0 }}>Family Tree Dashboard</h1>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <span>{user?.email}</span>
          <button
            onClick={() => navigate('/add')}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Add Person
          </button>
          <button
            onClick={handleSignOut}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Sign Out
          </button>
        </div>
      </header>

      {error && (
        <div style={{
          backgroundColor: '#fee',
          color: '#c00',
          padding: '1rem',
          margin: '1rem 2rem',
          borderRadius: '4px'
        }}>
          {error}
        </div>
      )}

      <div style={{ padding: '2rem', display: 'flex', gap: '2rem' }}>
        {/* Persons List */}
        <div style={{ flex: 1 }}>
          <h2>All Persons</h2>
          {persons.length === 0 ? (
            <p>No persons found. Click "Add Person" to create one.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {persons.map(person => (
                <div
                  key={person.id}
                  onClick={() => selectPerson(person)}
                  style={{
                    padding: '1rem',
                    backgroundColor: selectedPerson?.id === person.id ? '#e3f2fd' : 'white',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    border: selectedPerson?.id === person.id ? '2px solid #007bff' : '1px solid #ddd'
                  }}
                >
                  <div style={{ fontWeight: 'bold' }}>
                    {person.first_name} {person.last_name}
                  </div>
                  {person.birth_date && (
                    <div style={{ fontSize: '0.875rem', color: '#666' }}>
                      Born: {new Date(person.birth_date).toLocaleDateString()}
                    </div>
                  )}
                  {person.gender && (
                    <div style={{ fontSize: '0.875rem', color: '#666' }}>
                      Gender: {person.gender}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Selected Person Details */}
        {selectedPerson && (
          <div style={{ flex: 2 }}>
            <div style={{
              backgroundColor: 'white',
              padding: '1.5rem',
              borderRadius: '8px',
              boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2 style={{ margin: 0 }}>
                  {selectedPerson.first_name} {selectedPerson.last_name}
                </h2>
                <button
                  onClick={() => navigate(`/edit/${selectedPerson.id}`)}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Edit
                </button>
              </div>

              {selectedPerson.birth_date && (
                <p><strong>Birth Date:</strong> {new Date(selectedPerson.birth_date).toLocaleDateString()}</p>
              )}
              {selectedPerson.gender && (
                <p><strong>Gender:</strong> {selectedPerson.gender}</p>
              )}

              {/* Parents Section */}
              <div style={{ marginTop: '1.5rem' }}>
                <h3>Parents</h3>
                {selectedPerson.parents && selectedPerson.parents.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {selectedPerson.parents.map(parent => (
                      <div
                        key={parent.id}
                        onClick={() => {
                          const fullParent = persons.find(p => p.id === parent.id);
                          if (fullParent) selectPerson(fullParent);
                        }}
                        style={{
                          padding: '0.75rem',
                          backgroundColor: '#f8f9fa',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          border: '1px solid #dee2e6'
                        }}
                      >
                        ↑ {parent.first_name} {parent.last_name}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p style={{ color: '#666' }}>No parents added</p>
                )}
              </div>

              {/* Children Section */}
              <div style={{ marginTop: '1.5rem' }}>
                <h3>Children</h3>
                {selectedPerson.children && selectedPerson.children.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {selectedPerson.children.map(child => (
                      <div
                        key={child.id}
                        onClick={() => {
                          const fullChild = persons.find(p => p.id === child.id);
                          if (fullChild) selectPerson(fullChild);
                        }}
                        style={{
                          padding: '0.75rem',
                          backgroundColor: '#f8f9fa',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          border: '1px solid #dee2e6'
                        }}
                      >
                        ↓ {child.first_name} {child.last_name}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p style={{ color: '#666' }}>No children added</p>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
