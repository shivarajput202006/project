import { Fragment } from "react";

const TOKEN = /(`[^`]+`|\*\*[^*]+\*\*|\*[^*]+\*|\[[^\]]+\]\([^)]+\))/g;

export function parseInlineMarkdown(text, keyPrefix = "inline") {
  const result = [];
  let cursor = 0;
  for (const match of text.matchAll(TOKEN)) {
    const index = match.index ?? 0;
    if (index > cursor) result.push(text.slice(cursor, index));
    const token = match[0];
    if (token.startsWith("`")) {
      result.push(<code key={`${keyPrefix}-${index}`}>{token.slice(1, -1)}</code>);
    } else if (token.startsWith("**")) {
      result.push(<strong key={`${keyPrefix}-${index}`}>{token.slice(2, -2)}</strong>);
    } else if (token.startsWith("*")) {
      result.push(<em key={`${keyPrefix}-${index}`}>{token.slice(1, -1)}</em>);
    } else {
      const link = token.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
      let href;
      try {
        href = new URL(link?.[2] ?? "").href;
        if (!["http:", "https:"].includes(new URL(href).protocol)) href = null;
      } catch {
        href = null;
      }
      result.push(href
        ? <a key={`${keyPrefix}-${index}`} href={href} target="_blank" rel="noopener noreferrer">{link[1]}</a>
        : link?.[1] ?? token);
    }
    cursor = index + token.length;
  }
  if (cursor < text.length) result.push(text.slice(cursor));
  return result.map((part, index) => <Fragment key={`${keyPrefix}-part-${index}`}>{part}</Fragment>);
}

export function formatAssistantMarkdown(text, onCopyCode) {
  const blocks = String(text ?? "").split(/```([^\n]*)\n([\s\S]*?)```/g);
  if (blocks.length === 1) return formatTextBlocks(blocks[0]);
  const result = [];
  for (let index = 0; index < blocks.length; index += 3) {
    result.push(...formatTextBlocks(blocks[index]));
    if (index + 2 < blocks.length) {
      const language = blocks[index + 1].trim();
      const code = blocks[index + 2].replace(/\n$/, "");
      result.push(
        <div className="assistant-code-block" key={`code-${index}`}>
          <div><span>{language || "code"}</span><button type="button" onClick={() => onCopyCode(code)}>Copy</button></div>
          <pre><code>{code}</code></pre>
        </div>,
      );
    }
  }
  return result;
}

function formatTextBlocks(text) {
  return text.split(/\n{2,}/).filter(Boolean).map((block, index) => {
    const lines = block.split("\n");
    if (lines.every((line) => /^\s*[-*]\s+/.test(line))) {
      return <ul key={`list-${index}`}>{lines.map((line, lineIndex) => (
        <li key={`li-${lineIndex}`}>{parseInlineMarkdown(line.replace(/^\s*[-*]\s+/, ""), `li-${index}-${lineIndex}`)}</li>
      ))}</ul>;
    }
    if (lines.every((line) => /^\s*\d+\.\s+/.test(line))) {
      return <ol key={`ordered-${index}`}>{lines.map((line, lineIndex) => (
        <li key={`oli-${lineIndex}`}>{parseInlineMarkdown(line.replace(/^\s*\d+\.\s+/, ""), `oli-${index}-${lineIndex}`)}</li>
      ))}</ol>;
    }
    const heading = block.match(/^(#{1,3})\s+(.+)$/);
    if (heading) {
      const Heading = `h${heading[1].length}`;
      return <Heading key={`heading-${index}`}>{parseInlineMarkdown(heading[2], `heading-${index}`)}</Heading>;
    }
    return <p key={`paragraph-${index}`}>{lines.map((line, lineIndex) => (
      <Fragment key={`line-${index}-${lineIndex}`}>
        {lineIndex > 0 && <br />}
        {parseInlineMarkdown(line, `paragraph-${index}-${lineIndex}`)}
      </Fragment>
    ))}</p>;
  });
}
