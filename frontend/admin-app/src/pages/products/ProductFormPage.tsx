import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Form, Input, InputNumber, Switch, Button, Card, Spin, message, Space } from 'antd'
import { productsApi } from '../../api'
import type { CreateProductRequest } from '../../types'

function ProductFormPage() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id

  useEffect(() => {
    if (isEdit) {
      fetchProduct()
    }
  }, [id])

  const fetchProduct = async () => {
    setLoading(true)
    try {
      const response = await productsApi.getById(Number(id))
      if (response.success && response.data) {
        form.setFieldsValue(response.data)
      } else {
        message.error('상품 정보를 불러오는데 실패했습니다.')
        navigate('/products')
      }
    } catch {
      message.error('상품 정보를 불러오는데 실패했습니다.')
      navigate('/products')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (values: CreateProductRequest) => {
    setSaving(true)
    try {
      const response = isEdit
        ? await productsApi.update(Number(id), values)
        : await productsApi.create(values)

      if (response.success) {
        message.success(isEdit ? '상품이 수정되었습니다.' : '상품이 생성되었습니다.')
        navigate('/products')
      } else {
        message.error(response.message || '저장에 실패했습니다.')
      }
    } catch {
      message.error('저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">{isEdit ? '상품 수정' : '상품 추가'}</h1>
      </div>

      <Card className="form-container">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ displayOrder: 0, isHidden: false }}
        >
          <Form.Item
            name="name"
            label="상품명"
            rules={[{ required: true, message: '상품명을 입력해주세요' }]}
          >
            <Input placeholder="상품명을 입력하세요" />
          </Form.Item>

          <Form.Item name="category" label="카테고리">
            <Input placeholder="카테고리를 입력하세요" />
          </Form.Item>

          <Form.Item name="description" label="설명">
            <Input.TextArea rows={4} placeholder="상품 설명을 입력하세요" />
          </Form.Item>

          <Form.Item name="price" label="가격">
            <InputNumber
              style={{ width: '100%' }}
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value?.replace(/\$\s?|(,*)/g, '') as unknown as number}
              placeholder="가격을 입력하세요"
            />
          </Form.Item>

          <Form.Item name="imageUrl" label="이미지 URL">
            <Input placeholder="이미지 URL을 입력하세요" />
          </Form.Item>

          <Form.Item name="displayOrder" label="표시 순서">
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>

          <Form.Item name="isHidden" label="숨김 처리" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={saving}>
                {isEdit ? '수정' : '저장'}
              </Button>
              <Button onClick={() => navigate('/products')}>취소</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default ProductFormPage
