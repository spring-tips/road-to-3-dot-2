# Coordinated Restore at Checkpoint 


* [the support in Spring Framework itself ](https://docs.spring.io/spring-framework/reference/6.1/integration/checkpoint-restore.html)
* [a good article on runnig on CRaC with Zulu JDK ](https://aboullaite.me/what-the-crac/)
* [you need this dependency (org.crac/crac)](https://central.sonatype.com/artifact/org.crac/crac)
* install a proper CRaC compliant JDK: `sdk install java 21.crac-zulu && sdk default java 21.crac-zulu`
* [CRaC debugging](https://docs.azul.com/core/crac/crac-debugging)
* Make sure the JDK's `criu` tool is owned by `root`, per https://docs.azul.com/core/crac/crac-debugging
* https://webtechie.be/post/2023-06-15-crac-on-raspberry-pi/