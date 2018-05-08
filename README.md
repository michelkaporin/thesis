# Description

The system implements a proof-of-concept for secure in-memory index for processing time-series data. The development was carried out as part of the Master Thesis project in the Distributed Systems Group at ETH Zurich.

# Abstract

The growth of the Internet of Things (IoT) has resulted in the continuous production of massive amounts of data. Data comes in time-series of no standard format and often exhibits multiple dimensionalities, which creates a demand for efficient data storage and analysis systems. As collected IoT data is highly sensitive in its nature, it is vital to account for confidentiality when handling such data. Existing storage solutions trade off between characteristics of time-series, security and the need for its analytics. Hence, better secure systems for time-series data processing are required.

We present a system that allows to securely store, retrieve and run statistical and second dimension queries over time-series data. The system semantically organizes time-series in encrypted streams and aggregates its metadata to serve analytical queries efficiently. Our design protects metadata with encryption mechanisms that enable processing without disclosing information to untrusted entities. We provide a flexible architecture oblivious to the utilized data store for the collected data. Moreover, our solution allows extending the system with additional analysis functions and supporting other encryption schemes for secure computations.

Our solution shows orders of magnitude performance improvements in comparison to the Strawman scenario for securing time-series records. The system responds to temporal statistical queries within a constant time. We demonstrate that data ingestion rates to the encrypted streams are bounded by the persistent storage employed in the stream and not by the architecture of the system. Furthermore, the solution has a modest memory footprint that can be regulated by configuring system parameters.
