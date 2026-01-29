import { useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { Layout, Menu, Button, Dropdown, Avatar, Space, theme } from 'antd'
import {
  DashboardOutlined,
  ShoppingOutlined,
  FileTextOutlined,
  QuestionCircleOutlined,
  MessageOutlined,
  YoutubeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  SwapOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'
import { useAuth } from '../contexts/AuthContext'

const { Header, Sider, Content } = Layout

const menuItems: MenuProps['items'] = [
  {
    key: 'dashboard',
    icon: <DashboardOutlined />,
    label: '대시보드',
  },
  {
    key: 'products',
    icon: <ShoppingOutlined />,
    label: '상품 관리',
  },
  {
    key: 'boards',
    icon: <FileTextOutlined />,
    label: '게시판 관리',
  },
  {
    key: 'faqs',
    icon: <QuestionCircleOutlined />,
    label: 'FAQ 관리',
  },
  {
    key: 'qnas',
    icon: <MessageOutlined />,
    label: 'QnA 관리',
  },
  {
    key: 'youtube',
    icon: <YoutubeOutlined />,
    label: '유튜브 관리',
  },
]

function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const { user, logout, isSuperAdmin } = useAuth()
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken()

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    navigate(`/${e.key}`)
  }

  const userMenuItems: MenuProps['items'] = [
    ...(isSuperAdmin
      ? [
          {
            key: 'switch-company',
            icon: <SwapOutlined />,
            label: '회사 전환',
            onClick: () => navigate('/select-company'),
          },
        ]
      : []),
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '로그아웃',
      onClick: logout,
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontSize: collapsed ? 16 : 20,
            fontWeight: 'bold',
          }}
        >
          {collapsed ? 'BA' : 'Bear Admin'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['dashboard']}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 16, width: 64, height: 64 }}
          />
          <Space>
            {user?.companyName && (
              <span style={{ color: '#666' }}>{user.companyName}</span>
            )}
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} />
                <span>{user?.name || user?.username}</span>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            minHeight: 280,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default AdminLayout
