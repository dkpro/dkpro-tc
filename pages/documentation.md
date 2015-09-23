---
layout: page-fullwidth
title: "Documentation"
permalink: "/documentation/"
---

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}
{% assign unstable = (site.data.releases | where:"status", "unstable" | first) %}

## Reference Documentation

### DKPro TC 0.5.0

* [Getting Started](/dkpro-tc/DemoExperiments_0_5_0/)
* [Discriminators](/dkpro-tc/Discriminators_0_5_0/)

### DKPro TC 0.6.0

* [Getting Started](/dkpro-tc/DemoExperiments_0_6_0/)
* [Discriminators](/dkpro-tc/Discriminators_0_6_0/)

<!---
{% unless stable.version == null %}
### DKPro TC {{ stable.version }}
_latest release_

{% unless stable.user_guide_url == null %}* [User Guide]({{ stable.user_guide_url }}){% endunless %}
{% unless stable.developer_guide_url == null %}* [Developer Guide]({{ stable.developer_guide_url }}){% endunless %}
{% endunless %}
-->

{% unless unstable.version == null %}
### DKPro TC {{ unstable.version }} and {{ stable.version }}
_upcoming release - links may be temporarily broken while a build is in progress_

{% unless unstable.user_guide_url == null %}* [User Guide]({{ unstable.user_guide_url }}){% endunless %}
{% unless unstable.developer_guide_url == null %}* [Developer Guide]({{ unstable.developer_guide_url }}){% endunless %}
{% endunless %}
