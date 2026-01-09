import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error('Missing Supabase environment variables');
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

// Types for our database
export interface Person {
  id: string;
  created_at: string;
  first_name: string;
  last_name: string;
  birth_date?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  user_id: string;
}

export interface Relationship {
  id: string;
  created_at: string;
  parent_id: string;
  child_id: string;
  user_id: string;
}
