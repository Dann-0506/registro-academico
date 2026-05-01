import { createBrowserRouter, Navigate } from 'react-router-dom'
import { PrivateRoute } from './PrivateRoute'
import { AppLayout } from '@/layouts/AppLayout'
import Login from '@/pages/Login'
import Perfil from '@/pages/Perfil'
import Dashboard from '@/pages/admin/Dashboard'
import Alumnos from '@/pages/admin/Alumnos'
import Maestros from '@/pages/admin/Maestros'
import Administradores from '@/pages/admin/Administradores'
import Materias from '@/pages/admin/Materias'
import Grupos from '@/pages/admin/Grupos'
import Inscripciones from '@/pages/admin/Inscripciones'
import Configuracion from '@/pages/admin/Configuracion'
import CargaCSV from '@/pages/admin/CargaCSV'
import Respaldos from '@/pages/admin/Respaldos'
import MisGrupos from '@/pages/maestro/MisGrupos'
import GrupoDetalle from '@/pages/maestro/GrupoDetalle'
import MisCursos from '@/pages/alumno/MisCursos'
import CursoDetalle from '@/pages/alumno/CursoDetalle'

export const router = createBrowserRouter([
  { path: '/login', element: <Login /> },

  {
    element: <PrivateRoute />,
    children: [{
      element: <AppLayout />,
      children: [
        { path: '/perfil', element: <Perfil /> },

        // Admin
        { element: <PrivateRoute roles={['admin']} />, children: [
          { path: '/admin', element: <Dashboard /> },
          { path: '/admin/alumnos', element: <Alumnos /> },
          { path: '/admin/maestros', element: <Maestros /> },
          { path: '/admin/administradores', element: <Administradores /> },
          { path: '/admin/materias', element: <Materias /> },
          { path: '/admin/grupos', element: <Grupos /> },
          { path: '/admin/inscripciones', element: <Inscripciones /> },
          { path: '/admin/configuracion', element: <Configuracion /> },
          { path: '/admin/carga', element: <CargaCSV /> },
          { path: '/admin/respaldos', element: <Respaldos /> },
        ]},

        // Maestro
        { element: <PrivateRoute roles={['maestro']} />, children: [
          { path: '/maestro', element: <MisGrupos /> },
          { path: '/maestro/grupos/:id', element: <GrupoDetalle /> },
        ]},

        // Alumno
        { element: <PrivateRoute roles={['alumno']} />, children: [
          { path: '/alumno', element: <MisCursos /> },
          { path: '/alumno/cursos/:grupoId', element: <CursoDetalle /> },
        ]},
      ],
    }],
  },

  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '*', element: <Navigate to="/login" replace /> },
])
