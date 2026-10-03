const fs = require('node:fs');
const path = require('node:path');
const cp = require('node:child_process');
const root = path.resolve(__dirname, '..');
const walk = dir => fs.readdirSync(dir, {withFileTypes:true}).flatMap(entry => {
  const file = path.join(dir, entry.name);
  if (entry.name === 'private') return [];
  return entry.isDirectory() ? walk(file) : file.endsWith('.md') ? [file] : [];
});
const failures = [];
let checked = 0;
for (const file of [path.join(root, 'README.md'), ...walk(path.join(root, 'docs'))]) {
  const text = fs.readFileSync(file, 'utf8').replace(/```[\s\S]*?```/g, '');
  const targets = [...text.matchAll(/!?\[[^\]]*\]\((<[^>]+>|[^)]+)\)/g)].map(match => match[1].replace(/^<|>$/g, ''));
  targets.push(...[...text.matchAll(/<img\b[^>]*\bsrc=["']([^"']+)["']/gi)].map(match => match[1]));
  for (const raw of targets) {
    if (/^(https?:|mailto:|tel:|#|data:)/i.test(raw)) continue;
    if (/^(?:[A-Za-z]:|file:)/.test(raw)) { failures.push(`${path.relative(root,file)}: machine-specific link`); continue; }
    const target = decodeURIComponent(raw.split(/[?#]/)[0].replace(/\s+["'][^"']*["']$/, ''));
    if (!target) continue;
    const resolved = path.resolve(path.dirname(file), target);
    const relative = path.relative(root, resolved);
    checked++;
    if (relative.startsWith('..') || !fs.existsSync(resolved)) failures.push(`${path.relative(root,file)}: missing ${target}`);
    else if (cp.spawnSync('git',['check-ignore','--quiet','--',relative],{cwd:root}).status === 0) failures.push(`${path.relative(root,file)}: ignored ${target}`);
  }
}
if (failures.length) {
  process.stderr.write(failures.join('\n') + '\n');
  process.exitCode = 1;
} else console.log(`Documentation links: ${checked} local targets verified.`);
