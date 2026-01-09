# Frontend Quick Start Guide

This is a quick reference for getting the React frontend up and running.

## 1. Install Dependencies

```bash
cd frontend
npm install
```

## 2. Setup Supabase

1. Create a Supabase account at [supabase.com](https://supabase.com)
2. Create a new project
3. Run the SQL schema from `frontend/supabase/schema.sql` in the SQL Editor
4. Get your project URL and anon key from Settings → API

## 3. Configure Environment

```bash
cd frontend
cp .env.example .env
```

Edit `.env` and add your Supabase credentials:
```env
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your-anon-key-here
```

## 4. Run the App

```bash
npm run dev
```

Visit `http://localhost:5173`

## 5. Create an Account

1. Click "Don't have an account? Sign Up"
2. Enter email and password (min 6 characters)
3. Start building your family tree!

---

For detailed instructions, see [`frontend/SETUP.md`](frontend/SETUP.md)
