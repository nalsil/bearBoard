import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Form, Input, InputNumber, Switch, Button, Card, Spin, message, Space } from 'antd'
import { faqsApi } from '../../api'
import type { CreateFaqRequest } from '../../types'

function FaqFormPage() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id

  useEffect(() => {
    if (isEdit) {
      fetchFaq()
    }
  }, [id])

  const fetchFaq = async () => {
    setLoading(true)
    try {
      const response = await faqsApi.getById(Number(id))
      if (response.success && response.data) {
        form.setFieldsValue(response.data)
      } else {
        message.error('FAQ 정보를 불러오는데 실패했습니다.')
        navigate('/faqs')
      }
    } catch {
      message.error('FAQ 정보를 불러오는데 실패했습니다.')
      navigate('/faqs')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (values: CreateFaqRequest) => {
    setSaving(true)
    try {
      const response = isEdit
        ? await faqsApi.update(Number(id), values)
        : await faqsApi.create(values)

      if (response.success) {
        message.success(isEdit ? 'FAQ가 수정되었습니다.' : 'FAQ가 생성되었습니다.')
        navigate('/faqs')
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
        <h1 className="page-title">{isEdit ? 'FAQ 수정' : 'FAQ 추가'}</h1>
      </div>

      <Card className="form-container">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ displayOrder: 0, isHidden: false }}
        >
          <Form.Item name="category" label="카테고리">
            <Input placeholder="카테고리를 입력하세요" />
          </Form.Item>

          <Form.Item
            name="question"
            label="질문"
            rules={[{ required: true, message: '질문을 입력해주세요' }]}
          >
            <Input.TextArea rows={3} placeholder="질문을 입력하세요" />
          </Form.Item>

          <Form.Item
            name="answer"
            label="답변"
            rules={[{ required: true, message: '답변을 입력해주세요' }]}
          >
            <Input.TextArea rows={6} placeholder="답변을 입력하세요" />
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
              <Button onClick={() => navigate('/faqs')}>취소</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default FaqFormPage
