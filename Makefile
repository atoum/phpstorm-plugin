.PHONY: test

test:
	docker build -t atoum-phpstorm-plugin .
	docker run -ti -v $(PWD):/sources -w /sources atoum-phpstorm-plugin ./travis.sh

