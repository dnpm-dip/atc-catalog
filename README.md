# DNPM:DIP - ATC Catalog

ATC Catalog Component improved/generalized from bwHC for DNPM:DIP.

This component uses the ATC classification as provided in a spreadsheet by [WIdO](https://www.wido.de/publikationen-produkte/analytik/arzneimittel-klassifikation/).

The only processing that occurred is:

* Exporting the sheet sorted by ATC code to CSV
* DDD Entries spanning multiple rows in the original are enclosed in double-quotes in the exported CSV. For parsing purposes, such entries are post-processed using `vim` regex substition:
  * Remove linebreaks in quote-enclosed entries: `:%s/"\([^"]*\)\n\([^"]*\)"/"\1\2"/g`
  * Remove double-quotes: `:%s/"//g`
