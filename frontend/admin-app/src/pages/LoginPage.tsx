import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Card } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useAuth } from '../contexts/AuthContext'

interface LoginFormValues {
  username: string
  password: string
}

function LoginPage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { login } = useAuth()

  const handleSubmit = async (values: LoginFormValues) => {
    setLoading(true)
    try {
      const success = await login(values.username, values.password)
      if (success) {
        navigate('/dashboard')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <Card className="login-card">
        <h1 className="login-title">Bear Admin</h1>
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={handleSubmit}
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '아이디를 입력해주세요' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="아이디" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '비밀번호를 입력해주세요' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              로그인
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default LoginPage
