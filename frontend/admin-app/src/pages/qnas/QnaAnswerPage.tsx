import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Form, Input, Button, Card, Spin, message, Space, Descriptions, Tag } from 'antd'
import dayjs from 'dayjs'
import { qnasApi } from '../../api'
import type { Qna } from '../../types'

function QnaAnswerPage() {
  const [form] = Form.useForm()
  const [qna, setQna] = useState<Qna | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams()

  useEffect(() => {
    fetchQna()
  }, [id])

  const fetchQna = async () => {
    setLoading(true)
    try {
      const response = await qnasApi.getById(Number(id))
      if (response.success && response.data) {
        setQna(response.data)
        if (response.data.answerBody) {
          form.setFieldsValue({ answerBody: response.data.answerBody })
        }
      } else {
        message.error('Q&A 정보를 불러오는데 실패했습니다.')
        navigate('/qnas')
      }
    } catch {
      message.error('Q&A 정보를 불러오는데 실패했습니다.')
      navigate('/qnas')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (values: { answerBody: string }) => {
    setSaving(true)
    try {
      const response = await qnasApi.answer(Number(id), { answerBody: values.answerBody })
      if (response.success) {
        message.success('답변이 등록되었습니다.')
        navigate('/qnas')
      } else {
        message.error(response.message || '답변 등록에 실패했습니다.')
      }
    } catch {
      message.error('답변 등록에 실패했습니다.')
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

  if (!qna) {
    return null
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Q&A 답변</h1>
      </div>

      <Card title="질문 정보" style={{ marginBottom: 24 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="이메일">{qna.askerEmail}</Descriptions.Item>
          <Descriptions.Item label="작성일">
            {dayjs(qna.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="답변상태">
            <Tag color={qna.isAnswered ? 'green' : 'orange'}>
              {qna.isAnswered ? '답변완료' : '대기중'}
            </Tag>
          </Descriptions.Item>
          {qna.answeredAt && (
            <Descriptions.Item label="답변일">
              {dayjs(qna.answeredAt).format('YYYY-MM-DD HH:mm')}
            </Descriptions.Item>
          )}
          <Descriptions.Item label="공개상태">
            <Tag color={qna.isHidden ? 'default' : 'green'}>
              {qna.isHidden ? '숨김' : '공개'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>
        <div style={{ marginTop: 16 }}>
          <strong>질문 제목:</strong>
          <div
            style={{
              marginTop: 8,
              padding: 16,
              backgroundColor: '#f5f5f5',
              borderRadius: 4,
              fontWeight: 'bold',
            }}
          >
            {qna.questionTitle}
          </div>
        </div>
        <div style={{ marginTop: 16 }}>
          <strong>질문 내용:</strong>
          <div
            style={{
              marginTop: 8,
              padding: 16,
              backgroundColor: '#f5f5f5',
              borderRadius: 4,
              whiteSpace: 'pre-wrap',
            }}
          >
            {qna.questionBody}
          </div>
        </div>
      </Card>

      <Card title="답변 작성">
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="answerBody"
            label="답변"
            rules={[{ required: true, message: '답변을 입력해주세요' }]}
          >
            <Input.TextArea rows={8} placeholder="답변을 입력하세요" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={saving}>
                {qna.isAnswered ? '답변 수정' : '답변 등록'}
              </Button>
              <Button onClick={() => navigate('/qnas')}>목록</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default QnaAnswerPage
