import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Space, Tag, Popconfirm, message, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { productsApi } from '../../api'
import type { Product } from '../../types'

function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const navigate = useNavigate()

  const fetchProducts = async (page = 0, size = 20) => {
    setLoading(true)
    try {
      const response = await productsApi.getList(page, size)
      if (response.success && response.data) {
        setProducts(response.data.content)
        setPagination({
          current: response.data.page + 1,
          pageSize: response.data.size,
          total: response.data.totalElements,
        })
      }
    } catch {
      message.error('상품 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [])

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    fetchProducts((newPagination.current || 1) - 1, newPagination.pageSize)
  }

  const handleDelete = async (id: number) => {
    try {
      const response = await productsApi.delete(id)
      if (response.success) {
        message.success('상품이 삭제되었습니다.')
        fetchProducts((pagination.current || 1) - 1, pagination.pageSize)
      } else {
        message.error(response.message || '삭제에 실패했습니다.')
      }
    } catch {
      message.error('삭제에 실패했습니다.')
    }
  }

  const columns: ColumnsType<Product> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '상품명',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '카테고리',
      dataIndex: 'category',
      key: 'category',
      render: (category) => category || '-',
    },
    {
      title: '가격',
      dataIndex: 'price',
      key: 'price',
      render: (price) => (price ? `${Number(price).toLocaleString()}원` : '-'),
    },
    {
      title: '표시순서',
      dataIndex: 'displayOrder',
      key: 'displayOrder',
      width: 100,
    },
    {
      title: '상태',
      dataIndex: 'isHidden',
      key: 'isHidden',
      width: 100,
      render: (isHidden) => (
        <Tag color={isHidden ? 'default' : 'green'}>
          {isHidden ? '숨김' : '공개'}
        </Tag>
      ),
    },
    {
      title: '작업',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/products/${record.id}/edit`)}
          >
            수정
          </Button>
          <Popconfirm
            title="정말 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.id)}
            okText="삭제"
            cancelText="취소"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              삭제
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">상품 관리</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/products/new')}
        >
          상품 추가
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={products}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  )
}

export default ProductListPage
