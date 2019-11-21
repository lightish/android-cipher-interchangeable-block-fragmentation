## About this app

This Android application is an implementation of symmetric cryptographic algorithm based on the concept of variable block length using Android API 19.

The application uses algorithm to encrypt and decrypt files provided by device's file explorer.

Interface features a progress bar, which tracks encryption or decryption completion percentage.


## About the cryptographic algorithm

This algorithm is a modification of the one proposed in [this](https://scholar.google.com.ua/citations?user=oSePS9kAAAAJ&hl=ru#d=gs_md_cita-d&u=%2Fcitations%3Fview_op%3Dview_citation%26hl%3Dru%26user%3DoSePS9kAAAAJ%26cstart%3D20%26pagesize%3D80%26citation_for_view%3DoSePS9kAAAAJ%3Aye4kPcJQO24C%26tzom%3D-120) research utilizing the principles of block fragmentation change, meaning dynamic resizing of cryptographic primitives in various rounds of encryption.

It also is build on substitution–permutation network in Feistel structure.

It is determined that the developed algorithm has good stochastic and cryptographic properties.
