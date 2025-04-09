# Comic Info Editor

This project is a ComicInfo.xml editor for Comic Book files.

To run this project, you will need Java at last version 11, and you can get one on https://adoptium.net.

List of supported file formats:

* CB7
* CBR (read only)
* CBT
* CBZ

Supported image formats:

* Bitmap (BMP)
* Graphics Interchange Format (GIF)
* Joint Photographic Experts Group (JPG or JPEG)
* Portable Network Graphics (PNG)
* Tagged Image File Format (TIFF)

## Why not CBA?

CBA uses the ACE file format and this file is proprietary and have unfixable security issues.

## How can I create a Comic Book file?

A comic book file is just a compressed file with images. This file can have file and folders and the page orders is based on file name (sorted by number).

So, if you have a folder with images , just compress it (ZIP, for example) and change the name from ZIP do CBZ.

# Licensing

This software is released under the Apache License Version 2.0.
Additionally, Autoconf includes a licensing exception in some of its
source files.

For more licensing information, see
<http://www.apache.org/licenses/>.

-----
Copyright (C) 2025 Leonardo Alves da Costa.

Copying and distribution of this file, with or without modification,
are permitted in any medium without royalty provided the copyright
notice and this notice are preserved.  This file is offered as-is,
without warranty of any kind.
