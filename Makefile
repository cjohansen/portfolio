portfolio.jar: src/portfolio/*
	rm -f portfolio.jar && clojure -A:dev -M:jar

deploy: portfolio.jar
	mvn deploy:deploy-file -Dfile=portfolio.jar -DrepositoryId=clojars -Durl=https://clojars.org/repo -DpomFile=pom.xml

.PHONY: deploy
