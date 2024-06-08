rm -rf docs/index.md
rm -rf docs/change_log.md
cp ../README.md docs/index.md
cp ../CHANGELOG.md docs/change_log.md

python3 change_index_path.py