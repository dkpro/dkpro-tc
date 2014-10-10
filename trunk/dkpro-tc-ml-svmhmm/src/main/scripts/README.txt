For deploying on Zoidberg -> public-model-releases-local

1.) Make sure you have credentials in your .m2/settings.xml for accessing Zoidberg
(see internal UKP wiki)

2.) Run Ant with parameter:
$ant remote-maven -Dalt.maven.repo.url=https://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local
