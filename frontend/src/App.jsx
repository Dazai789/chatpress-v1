import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import List from './pages/List';
import Form from './pages/Form';
import Detail from './pages/Detail';
import Import from './pages/Import';
import Logs from './pages/Logs';
import Layout from './components/Layout';

function PrivateRoute({ children }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
          <Route index element={<Navigate to="/artifacts" />} />
          <Route path="artifacts" element={<List />} />
          <Route path="artifacts/new" element={<Form />} />
          <Route path="artifacts/import" element={<Import />} />
          <Route path="artifacts/:id" element={<Detail />} />
          <Route path="artifacts/:id/edit" element={<Form />} />
          <Route path="logs" element={<Logs />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
