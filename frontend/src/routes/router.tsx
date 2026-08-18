import { createBrowserRouter, Navigate } from 'react-router-dom'
import { App } from '../App'
import { HomePage } from '../pages/HomePage'
import { LoginPage } from '../pages/LoginPage'
import { SignupPage } from '../pages/SignupPage'
import { PublicOnlyRoute } from './PublicOnlyRoute'
import { RequireAuth } from './RequireAuth'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        path: 'login',
        element: (
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        ),
      },
      {
        path: 'signup',
        element: (
          <PublicOnlyRoute>
            <SignupPage />
          </PublicOnlyRoute>
        ),
      },
      {
        element: <RequireAuth />,
        children: [
          {
            index: true,
            element: <HomePage />,
          },
        ],
      },
      {
        path: '*',
        element: <Navigate to="/" replace />,
      },
    ],
  },
])
