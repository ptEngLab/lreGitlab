from pathlib import Path
import re

# Regex matches <!-- include: path/to/file -->
pattern = re.compile(r"<!-- include:\s*(.*?) -->")

def expand_includes(md_file: Path):
    text = md_file.read_text(encoding="utf-8")
    matches = pattern.findall(text)
    for filename in matches:
        snippet_path = Path(filename)
        if snippet_path.exists():
            snippet = snippet_path.read_text(encoding="utf-8")
            fenced = f"```yaml\n{snippet}\n```"
            text = text.replace(f"<!-- include: {filename} -->", fenced)
    md_file.write_text(text, encoding="utf-8")

def main():
    for md_file in Path("ReadMe").rglob("*.md"):
        expand_includes(md_file)

if __name__ == "__main__":
    main()
