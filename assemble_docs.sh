set -e
rm -rf doc/docs/reference
./gradlew clean dokkaHtmlMultiModule

rm -rf doc/docs/index.md
rm -rf doc/docs/change_log.md
cp README.md doc/docs/index.md
cp CHANGELOG.md doc/docs/change_log.md

python3 doc/change_index_path.py