SHELL = /bin/bash
build : copyfile assign-permissions
copyfile :
	if [ -d "../.git/hooks" ]; then \
		cp -f pre-push ../.git/hooks; \
	fi
assign-permissions :
	if [ -e "../.git/hooks/pre-push" ]; then \
		chmod +x ../.git/hooks/pre-push;  \
	fi
