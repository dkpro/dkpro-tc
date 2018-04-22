---
layout: page-fullwidth
title: "Documentation"
permalink: "/documentation/"
---

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}
{% assign unstable = (site.data.releases | where:"status", "unstable" | first) %}

## Reference Documentation

{% unless stable.version == null %}
### DKPro TC {{ stable.version }}
_latest release_

{% unless stable.user_guide_url == null %}* [User Guide]({{ stable.user_guide_url }}){% endunless %}
{% unless stable.developer_guide_url == null %}* [Developer Guide]({{ stable.developer_guide_url }}){% endunless %}
{% endunless %}

{% unless unstable.version == null %}
### DKPro TC {{ unstable.version }}
* [Setting Up DKProTC](/dkpro-tc/SettingUpDKPro/)

* [Basics](/dkpro-tc/DKProTcBasics_1_0_0/)
* [Processing Modes](/dkpro-tc/DKProTcProcessingModes/)

* [Using DKPro Core Readers](/dkpro-tc/DKProTcUsingDKProCoreReaders/)
* [Wiring Experiments](/dkpro-tc/DKProTcWiringExperiments_1_0_0/)

* [Deep Learning](/dkpro-tc/DKProTcDeepLearning_1_0_0/)
* [Debugging Deep Learning Experiments](/dkpro-tc/DKProTcDebugginPythonDeepLearningExperiments/)

_upcoming release - links may be temporarily broken while a build is in progress_

{% unless unstable.user_guide_url == null %}* [User Guide]({{ unstable.user_guide_url }}){% endunless %}
{% unless unstable.developer_guide_url == null %}* [Developer Guide]({{ unstable.developer_guide_url }}){% endunless %}
{% endunless %}
