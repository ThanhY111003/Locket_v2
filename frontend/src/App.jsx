import React, { useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import FeedPage from './pages/FeedPage'
import UploadPage from './pages/UploadPage'
import ReportPage from './pages/ReportPage'
import FriendsPage from './pages/FriendsPage'

import OAuth2CallbackPage from './pages/OAuth2CallbackPage'

import AdminPage from './pages/AdminPage'
import AdminLoginPage from './pages/AdminLoginPage'

// Simulate auth via localStorage
const isAuthenticated = () => {
  return localStorage.getItem('token') !== null
}

function ProtectedLayout() {
  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <Routes>
          <Route path="/feed" element={<FeedPage />} />
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/report" element={<ReportPage />} />
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="*" element={<Navigate to="/feed" replace />} />
        </Routes>
      </main>
    </div>
  )
}

function AuthGuard({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }
  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/admin/login" element={<AdminLoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route
          path="/*"
          element={
            <AuthGuard>
              <ProtectedLayout />
            </AuthGuard>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
