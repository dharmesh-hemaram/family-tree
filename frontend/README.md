# Family Tree Frontend

A React TypeScript application for managing family tree relationships with Supabase authentication.

## Features

- **Authentication**: Login/Signup with Supabase
- **Dashboard**: View all persons in your family tree
- **Person Management**: Add, edit, and delete family members
- **Relationship Management**: Define parent-child relationships
- **Navigation**: Explore family connections by clicking on parents/children

## Setup

### Prerequisites

- Node.js 16+ and npm
- Supabase account and project

### Installation

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   ```

3. Update `.env` with your Supabase credentials:
   ```
   VITE_SUPABASE_URL=https://xihluajvxesgatewmlcg.supabase.co
   VITE_SUPABASE_ANON_KEY=your_actual_publishable_key
   ```

### Database Setup

Run the SQL schema in your Supabase project:

1. Go to your Supabase project dashboard
2. Navigate to the SQL Editor
3. Copy and paste the contents of `supabase/schema.sql`
4. Run the query to create tables and policies

This will create:
- `persons` table for storing family members
- `relationships` table for parent-child connections
- Row Level Security (RLS) policies to ensure users only see their own data
- Indexes for better performance

### Running the Application

Development mode:
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

Build for production:
```bash
npm run build
```

Preview production build:
```bash
npm run preview
```

## Usage

### Login/Signup

1. Navigate to the application
2. Enter your email and password
3. Click "Sign Up" to create a new account or "Login" if you already have one

### Dashboard

- View all persons in your family tree
- Click on a person to see their details, parents, and children
- Click on parent/child names to navigate to that person
- Click "Add Person" to create a new family member
- Click "Edit" to modify a person's information

### Adding a Person

1. Click "Add Person" from the dashboard
2. Fill in:
   - First Name (required)
   - Last Name (required)
   - Birth Date (optional)
   - Gender (optional)
   - Parents (select from existing persons)
   - Children (select from existing persons)
3. Click "Create Person"

### Editing a Person

1. Click on a person in the dashboard
2. Click the "Edit" button
3. Modify the information
4. Update relationships by checking/unchecking parents and children
5. Click "Update Person" to save changes
6. Or click "Delete" to remove the person (this will also delete associated relationships)

## Architecture

### Technology Stack

- **React 18**: UI framework
- **TypeScript**: Type safety
- **Vite**: Build tool and dev server
- **React Router**: Client-side routing
- **Supabase**: Backend-as-a-Service for authentication and database

### Project Structure

```
frontend/
├── public/          # Static assets
├── src/
│   ├── components/  # Reusable components
│   │   └── ProtectedRoute.tsx
│   ├── contexts/    # React contexts
│   │   └── AuthContext.tsx
│   ├── lib/         # Utilities and configurations
│   │   └── supabase.ts
│   ├── pages/       # Page components
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   └── AddEditPersonPage.tsx
│   ├── App.tsx      # Main app component with routes
│   ├── main.tsx     # Application entry point
│   └── index.css    # Global styles
├── supabase/
│   └── schema.sql   # Database schema
├── .env             # Environment variables (not committed)
├── .env.example     # Example environment variables
└── package.json
```

### Key Components

- **AuthContext**: Manages authentication state and provides auth methods
- **ProtectedRoute**: Ensures only authenticated users can access certain pages
- **LoginPage**: Handles user authentication
- **DashboardPage**: Main view showing family tree with navigation
- **AddEditPersonPage**: Form for creating/updating persons and relationships

## Security

- Row Level Security (RLS) ensures users can only access their own data
- Authentication handled securely by Supabase
- All database queries are scoped to the authenticated user

## Future Enhancements

- Visual family tree diagram
- Search and filter functionality
- Import/export GEDCOM files
- Photo uploads for family members
- Timeline view of family events
- Shareable family trees

