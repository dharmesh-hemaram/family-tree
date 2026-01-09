-- Family Tree Database Schema for Supabase

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create persons table
CREATE TABLE IF NOT EXISTS persons (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  birth_date DATE,
  gender TEXT CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE
);

-- Create relationships table (for parent-child relationships)
CREATE TABLE IF NOT EXISTS relationships (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  parent_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
  child_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  UNIQUE(parent_id, child_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_persons_user_id ON persons(user_id);
CREATE INDEX IF NOT EXISTS idx_persons_created_at ON persons(created_at);
CREATE INDEX IF NOT EXISTS idx_relationships_user_id ON relationships(user_id);
CREATE INDEX IF NOT EXISTS idx_relationships_parent_id ON relationships(parent_id);
CREATE INDEX IF NOT EXISTS idx_relationships_child_id ON relationships(child_id);

-- Enable Row Level Security (RLS)
ALTER TABLE persons ENABLE ROW LEVEL SECURITY;
ALTER TABLE relationships ENABLE ROW LEVEL SECURITY;

-- Create policies for persons table
-- Users can only see their own persons
CREATE POLICY "Users can view own persons" ON persons
  FOR SELECT USING (auth.uid() = user_id);

-- Users can insert their own persons
CREATE POLICY "Users can insert own persons" ON persons
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Users can update their own persons
CREATE POLICY "Users can update own persons" ON persons
  FOR UPDATE USING (auth.uid() = user_id);

-- Users can delete their own persons
CREATE POLICY "Users can delete own persons" ON persons
  FOR DELETE USING (auth.uid() = user_id);

-- Create policies for relationships table
-- Users can only see their own relationships
CREATE POLICY "Users can view own relationships" ON relationships
  FOR SELECT USING (auth.uid() = user_id);

-- Users can insert their own relationships
CREATE POLICY "Users can insert own relationships" ON relationships
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Users can delete their own relationships
CREATE POLICY "Users can delete own relationships" ON relationships
  FOR DELETE USING (auth.uid() = user_id);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for persons table
CREATE TRIGGER update_persons_updated_at BEFORE UPDATE ON persons
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
