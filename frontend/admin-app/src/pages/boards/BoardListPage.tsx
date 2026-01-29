import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Card, message } from 'antd'
import { FileTextOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { boardsApi } from '../../api'
import type { Board } from '../../types'

function BoardListPage() {
  const [boards, setBoards] = useState<Board[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    fetchBoards()
  }, [])

  const fetchBoards = async () => {
    setLoading(true)
    try {
      const response = await boardsApi.getList()
      if (response.success && response.data) {
        setBoards(response.data)
      }
    } catch {
      message.error('게시판 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const columns: ColumnsType<Board> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '게시판명',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '타입',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '작업',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Button
          type="link"
          icon={<FileTextOutlined />}
          onClick={() => navigate(`/boards/${record.id}/posts`)}
        >
          게시글 관리
        </Button>
      ),
    },
  ]

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">게시판 관리</h1>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={boards}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>
    </div>
  )
}

export default BoardListPage
