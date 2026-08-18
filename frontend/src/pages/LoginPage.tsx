import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiClientError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { login } from '../features/auth/api'

interface FormValues {
  email: string
  password: string
}

type FieldErrors = Partial<Record<keyof FormValues, string>>

interface LoginLocationState {
  signupSuccess?: boolean
  from?: {
    pathname?: string
  }
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(values: FormValues): FieldErrors {
  const errors: FieldErrors = {}
  const email = values.email.trim()

  if (!email) {
    errors.email = '이메일을 입력해 주세요.'
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = '올바른 이메일 형식을 입력해 주세요.'
  }
  if (!values.password) {
    errors.password = '비밀번호를 입력해 주세요.'
  }

  return errors
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setAccessToken } = useAuth()
  const [values, setValues] = useState<FormValues>({ email: '', password: '' })
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const locationState = location.state as LoginLocationState | null

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const errors = validate(values)
    setFieldErrors(errors)
    setFormError(null)

    if (Object.keys(errors).length > 0) {
      return
    }

    setIsSubmitting(true)
    try {
      const response = await login({
        email: values.email.trim(),
        password: values.password,
      })
      setAccessToken(response.accessToken)

      navigate(locationState?.from?.pathname ?? '/', { replace: true })
    } catch (error) {
      if (error instanceof ApiClientError) {
        const fields = error.details?.fields
        setFieldErrors({
          email: fields?.email,
          password: fields?.password,
        })
        setFormError(
          error.details?.code === 'INVALID_CREDENTIALS'
            ? '이메일 또는 비밀번호를 확인해 주세요.'
            : (error.details?.message ?? '로그인 요청을 처리하지 못했습니다.'),
        )
      } else {
        setFormError('네트워크 연결을 확인한 뒤 다시 시도해 주세요.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="flex min-h-dvh flex-col px-6 pb-8 pt-12">
      <header>
        <p className="text-xs font-semibold tracking-[0.18em] text-neutral-500 uppercase">
          RoutineLog
        </p>
        <h1 className="mt-8 text-4xl font-semibold tracking-[-0.04em] text-neutral-950">
          다시 시작해요.
        </h1>
        <p className="mt-3 text-sm leading-6 text-neutral-500">
          이메일과 비밀번호로 로그인해 주세요.
        </p>
      </header>

      {locationState?.signupSuccess && (
        <div
          className="mt-8 rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm leading-5 text-neutral-700"
          role="status"
        >
          회원가입이 완료되었습니다. 로그인해 주세요.
        </div>
      )}

      <form className="mt-12 flex flex-1 flex-col" noValidate onSubmit={handleSubmit}>
        <div className="space-y-6">
          <div>
            <label className="text-sm font-medium text-neutral-800" htmlFor="email">
              이메일
            </label>
            <input
              autoComplete="email"
              className="mt-2 min-h-12 w-full rounded-xl border border-neutral-200 bg-white px-4 text-base text-neutral-950 outline-none transition focus:border-neutral-950 focus:ring-2 focus:ring-neutral-950/10"
              id="email"
              inputMode="email"
              name="email"
              onChange={(event) => {
                setValues((current) => ({ ...current, email: event.target.value }))
                setFieldErrors((current) => ({ ...current, email: undefined }))
              }}
              type="email"
              value={values.email}
              aria-describedby={fieldErrors.email ? 'email-error' : undefined}
              aria-invalid={Boolean(fieldErrors.email)}
            />
            {fieldErrors.email && (
              <p className="mt-2 text-sm text-neutral-600" id="email-error">
                {fieldErrors.email}
              </p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium text-neutral-800" htmlFor="password">
              비밀번호
            </label>
            <input
              autoComplete="current-password"
              className="mt-2 min-h-12 w-full rounded-xl border border-neutral-200 bg-white px-4 text-base text-neutral-950 outline-none transition focus:border-neutral-950 focus:ring-2 focus:ring-neutral-950/10"
              id="password"
              name="password"
              onChange={(event) => {
                setValues((current) => ({ ...current, password: event.target.value }))
                setFieldErrors((current) => ({ ...current, password: undefined }))
              }}
              type="password"
              value={values.password}
              aria-describedby={fieldErrors.password ? 'password-error' : undefined}
              aria-invalid={Boolean(fieldErrors.password)}
            />
            {fieldErrors.password && (
              <p className="mt-2 text-sm text-neutral-600" id="password-error">
                {fieldErrors.password}
              </p>
            )}
          </div>
        </div>

        {formError && (
          <div
            className="mt-6 rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm leading-5 text-neutral-700"
            role="alert"
          >
            {formError}
          </div>
        )}

        <button
          className="mt-auto min-h-13 w-full rounded-xl bg-neutral-950 px-5 py-3.5 text-base font-semibold text-white transition hover:bg-neutral-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-neutral-950 disabled:cursor-not-allowed disabled:bg-neutral-300"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting ? '로그인 중...' : '로그인'}
        </button>
        <p className="mt-5 text-center text-sm text-neutral-500">
          아직 계정이 없나요?{' '}
          <Link className="font-semibold text-neutral-950 underline-offset-4 hover:underline" to="/signup">
            회원가입
          </Link>
        </p>
      </form>
    </main>
  )
}
