cd /Users/vladimirpopov/Documents/lab/factoring/factor-portal
git pull
npm run build
cp -r build ~/Documents/lab/factoring/backend/src/main/resources/
cd ~/Documents/lab/factoring/backend/
sbt 'set test in assembly := {}' clean assembly
cd ~/Blockchain/HLF/fabric-starter
docker-machine scp -r ~/Blockchain/HLF/fabric-starter/backend factoringdev-buyer:.
eval "$(docker-machine env factoringdev-buyer)"
docker restart backend.buyer.factoring.ru
docker-machine scp -r ~/Blockchain/HLF/fabric-starter/backend factoringdev-factor:.
eval "$(docker-machine env factoringdev-factor)"
docker restart backend.factor.factoring.ru