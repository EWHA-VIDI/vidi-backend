const types = [
  'feat',
  'fix',
  'style',
  'refactor',
  'docs',
  'test',
  'chore',
  'build',
  'ci',
  'perf',
  'revert',
  'infra',
  'release',
]
const pattern = new RegExp(
  `^(${types.join('|')})(?:\\([a-z0-9][a-z0-9-]*\\))?!?: ([^\\r\\n]+) \\[VIDI-[1-9]\\d*\\]$`,
)

function validateTitle(title) {
  const match = pattern.exec(title)
  return Boolean(
    match &&
    title.length <= 120 &&
    title === title.trim() &&
    match[2].trim() === match[2] &&
    !match[2].endsWith('.') &&
    (title.match(/VIDI-\d+/g) ?? []).length === 1,
  )
}

module.exports = { types, validateTitle }

if (require.main === module && !validateTitle(process.env.PR_TITLE ?? process.argv[2] ?? '')) {
  console.error(
    'type(scope): 작업 내용 [VIDI-123] 형식을 사용한다. scope는 선택이며 Jira 키는 하나만 사용한다.',
  )
  process.exitCode = 1
}
