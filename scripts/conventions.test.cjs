const { test } = require('node:test')
const assert = require('node:assert/strict')
const { types, validateTitle } = require('./conventions.cjs')

for (const type of types) {
  test(`${type}: optional scope and breaking change`, () => {
    assert.equal(validateTitle(`${type}: 설정을 정리 [VIDI-9]`), true)
    assert.equal(validateTitle(`${type}(config)!: 설정을 정리 [VIDI-9]`), true)
  })
}
for (const title of [
  '[CHORE] 설정',
  'ci: 설정',
  'CI: 설정 [VIDI-9]',
  'hotfix: 설정 [VIDI-9]',
  'ci: 설정 [vidi-9]',
  'ci: 설정 [VIDI-0]',
  'ci: 설정 [VIDI-9] [VIDI-12]',
  'ci: 설정. [VIDI-9]',
  'ci:  설정 [VIDI-9]',
  'ci: 설정 [VIDI-9]\n',
  'ci: 설정\n추가 [VIDI-9]',
]) {
  test(`reject ${JSON.stringify(title)}`, () => assert.equal(validateTitle(title), false))
}

test('reject long title', () =>
  assert.equal(validateTitle('ci: ' + '가'.repeat(121) + ' [VIDI-13]'), false))
