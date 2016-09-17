Nested Data Source
=====================
Key-Value Pair
--------------
Usually, key-value pair is used to store data. It's very easy to retrieve data from a key-value pair container by specified key. In Java, key-value pair container is known as Map.<br/>
If there are a large amount of data, key is separated into multiple levels to avoid duplication.<br/>

Data Source
-----------
No matter key-value pair or any other data structure, a physical structure should be used as target the data save to and load from. This process is always known as serialization. The physical structure is source of the data.<br/>

Nested Data Source
------------------
If key-value pair whose key is multiple level is serialized in a data source, it can be considered as a series of data sources which are nested one by another one.<br/>
This project defines an interface describing nested data source. File system and data nestable file format (XML, JSON, Properties) are used as implementation of such interface.<br/>
