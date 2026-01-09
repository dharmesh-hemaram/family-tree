# Family Tree Frontend - Setup Instructions

This document provides step-by-step instructions for setting up the Family Tree frontend application with Supabase.

## Prerequisites

- Node.js 16 or higher
- npm or yarn
- A Supabase account (free tier is sufficient)

## Step 1: Create a Supabase Project

1. Go to [https://supabase.com](https://supabase.com) and sign in/sign up
2. Click "New Project"
3. Fill in the project details:
   - Name: `family-tree` (or any name you prefer)
   - Database Password: Choose a strong password
   - Region: Select the closest region to you
4. Wait for the project to be created (this may take a few minutes)

## Step 2: Set Up the Database Schema

1. In your Supabase project dashboard, navigate to the **SQL Editor**
2. Click "New query"
3. Copy the contents of `frontend/supabase/schema.sql`
4. Paste it into the SQL editor
5. Click "Run" or press `Ctrl+Enter` to execute the query
6. You should see a success message indicating tables were created

This creates:
- `persons` table for storing family members
- `relationships` table for parent-child connections
- Row Level Security (RLS) policies
- Database indexes for performance

## Step 3: Get Your Supabase Credentials

1. In your Supabase project dashboard, go to **Settings** → **API**
2. You'll find:
   - **Project URL**: Something like `https://xihluajvxesgatewmlcg.supabase.co`
   - **Anon/Public Key**: A long string starting with `eyJ...`
3. Keep these values handy for the next step

## Step 4: Configure the Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

3. Edit `.env` and update with your Supabase credentials:
   ```env
   VITE_SUPABASE_URL=https://your-project-url.supabase.co
   VITE_SUPABASE_ANON_KEY=your-anon-key-here
   ```

## Step 5: Install Dependencies

```bash
npm install
```

This will install:
- React and React Router
- TypeScript
- Supabase client library
- Vite build tool

## Step 6: Run the Development Server

```bash
npm run dev
```

The application will start on `http://localhost:5173` (or another port if 5173 is busy).

## Step 7: Test the Application

1. Open your browser and navigate to `http://localhost:5173`
2. You should be redirected to the login page
3. Click "Don't have an account? Sign Up"
4. Create a new account with your email and password (minimum 6 characters)
5. After signing up, you'll be redirected to the dashboard
6. Try creating persons and relationships!

## Troubleshooting

### "Missing Supabase environment variables" error

- Make sure you've created the `.env` file in the `frontend` directory
- Verify that the variable names start with `VITE_` prefix
- Restart the dev server after changing environment variables

### Authentication errors

- Verify your Supabase project URL and anon key are correct
- Make sure you've run the database schema SQL
- Check that email confirmations are disabled (or enabled based on your preference) in Supabase Auth settings

### Database errors when creating persons

- Ensure you've run the schema.sql file completely
- Check that Row Level Security policies were created successfully
- Verify you're logged in with a valid user account

### Build errors

- Make sure you're using Node.js 16 or higher
- Delete `node_modules` and `package-lock.json`, then run `npm install` again
- Check that all imports use `type` prefix for type-only imports

## Building for Production

To create a production build:

```bash
npm run build
```

The built files will be in the `dist` directory. You can serve them with:

```bash
npm run preview
```

Or deploy to any static hosting service (Vercel, Netlify, GitHub Pages, etc.).

## Next Steps

Now that your application is running:

1. **Customize the UI**: Update styles in the page components
2. **Add more fields**: Extend the Person model with additional attributes
3. **Implement search**: Add search functionality to find persons
4. **Create visualizations**: Add a family tree diagram using a library like D3.js or vis.js
5. **Add photos**: Implement photo uploads using Supabase Storage

## Security Notes

- The provided Supabase URL and key in the problem statement are placeholders
- Always use your own Supabase project credentials
- Never commit `.env` files to version control
- Row Level Security ensures users can only access their own data
- All database operations are automatically scoped to the authenticated user

## Support

For issues or questions:
- Check the [Supabase Documentation](https://supabase.com/docs)
- Review the [React Documentation](https://react.dev)
- Consult the frontend README.md for architecture details
