import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiClientError } from '../api/client'
import { signup } from '../features/auth/api'

interface FormValues {
  name: string
  email: string
  password: string
}

type FieldErrors = Partial<Record<keyof FormValues, string>>

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(values: FormValues): FieldErrors {
  const errors: FieldErrors = {}
  const name = values.name.trim()
  const email = values.email.trim()

  if (!name) {
    errors.name = '이름을 입력해 주세요.'
  } else if (name.length > 50) {
    errors.name = '이름은 50자 이하로 입력해 주세요.'
  }

  if (!email) {
    errors.email = '이메일을 입력해 주세요.'
  } else if (email.length > 255) {
    errors.email = '이메일은 255자 이하로 입력해 주세요.'
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = '올바른 이메일 형식을 입력해 주세요.'
  }

  if (!values.password) {
    errors.password = '비밀번호를 입력해 주세요.'
  }

  return errors
}

export function SignupPage() {
  const navigate = useNavigate()
  const [values, setValues] = useState<FormValues>({ name: '', email: '', password: '' })
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

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
      await signup({
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
      })
      navigate('/login', {
        replace: true,
        state: { signupSuccess: true },
      })
    } catch (error) {
      if (error instanceof ApiClientError) {
        const fields = error.details?.fields
        setFieldErrors({
          name: fields?.name,
          email: fields?.email,
          password: fields?.password,
        })
        setFormError(
          error.details?.code === 'EMAIL_ALREADY_EXISTS'
            ? '이미 사용 중인 이메일입니다.'
            : (error.details?.message ?? '회원가입 요청을 처리하지 못했습니다.'),
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
          함께 시작해요.
        </h1>
        <p className="mt-3 text-sm leading-6 text-neutral-500">
          RoutineLog에 사용할 정보를 입력해 주세요.
        </p>
      </header>

      <form className="mt-10 flex flex-1 flex-col" noValidate onSubmit={handleSubmit}>
        <div className="space-y-5">
          <div>
            <label className="text-sm font-medium text-neutral-800" htmlFor="name">
              이름
            </label>
            <input
              autoComplete="name"
              className="mt-2 min-h-12 w-full rounded-xl border border-neutral-200 bg-white px-4 text-base text-neutral-950 outline-none transition focus:border-neutral-950 focus:ring-2 focus:ring-neutral-950/10"
              id="name"
              maxLength={50}
              name="name"
              onChange={(event) => {
                setValues((current) => ({ ...current, name: event.target.value }))
                setFieldErrors((current) => ({ ...current, name: undefined }))
              }}
              value={values.name}
              aria-describedby={fieldErrors.name ? 'name-error' : undefined}
              aria-invalid={Boolean(fieldErrors.name)}
            />
            {fieldErrors.name && (
              <p className="mt-2 text-sm text-neutral-600" id="name-error">
                {fieldErrors.name}
              </p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium text-neutral-800" htmlFor="signup-email">
              이메일
            </label>
            <input
              autoComplete="email"
              className="mt-2 min-h-12 w-full rounded-xl border border-neutral-200 bg-white px-4 text-base text-neutral-950 outline-none transition focus:border-neutral-950 focus:ring-2 focus:ring-neutral-950/10"
              id="signup-email"
              inputMode="email"
              maxLength={255}
              name="email"
              onChange={(event) => {
                setValues((current) => ({ ...current, email: event.target.value }))
                setFieldErrors((current) => ({ ...current, email: undefined }))
              }}
              type="email"
              value={values.email}
              aria-describedby={fieldErrors.email ? 'signup-email-error' : undefined}
              aria-invalid={Boolean(fieldErrors.email)}
            />
            {fieldErrors.email && (
              <p className="mt-2 text-sm text-neutral-600" id="signup-email-error">
                {fieldErrors.email}
              </p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium text-neutral-800" htmlFor="signup-password">
              비밀번호
            </label>
            <input
              autoComplete="new-password"
              className="mt-2 min-h-12 w-full rounded-xl border border-neutral-200 bg-white px-4 text-base text-neutral-950 outline-none transition focus:border-neutral-950 focus:ring-2 focus:ring-neutral-950/10"
              id="signup-password"
              name="password"
              onChange={(event) => {
                setValues((current) => ({ ...current, password: event.target.value }))
                setFieldErrors((current) => ({ ...current, password: undefined }))
              }}
              type="password"
              value={values.password}
              aria-describedby={fieldErrors.password ? 'signup-password-error' : undefined}
              aria-invalid={Boolean(fieldErrors.password)}
            />
            {fieldErrors.password && (
              <p className="mt-2 text-sm text-neutral-600" id="signup-password-error">
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

        <div className="mt-auto pt-10">
          <button
            className="min-h-13 w-full rounded-xl bg-neutral-950 px-5 py-3.5 text-base font-semibold text-white transition hover:bg-neutral-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-neutral-950 disabled:cursor-not-allowed disabled:bg-neutral-300"
            disabled={isSubmitting}
            type="submit"
          >
            {isSubmitting ? '가입 중...' : '회원가입'}
          </button>
          <p className="mt-5 text-center text-sm text-neutral-500">
            이미 계정이 있나요?{' '}
            <Link className="font-semibold text-neutral-950 underline-offset-4 hover:underline" to="/login">
              로그인
            </Link>
          </p>
        </div>
      </form>
    </main>
  )
}
