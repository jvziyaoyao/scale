# 刷新dokka文档
./assemble_dokka.sh

# 复制首页的文档
./copy_root_doc.sh

# 发布到分支
python3 -m mkdocs gh-deploy --force