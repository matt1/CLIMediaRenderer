About
=====
Cli Media Renderer is a command-line UPnP audio media renderer/DNLA DMR design for headless operation.  It is not a media server or a control point!  You cant host media or control playback directly using Cli Media Renderer, it is designed specifically for just playing audio from network sources via remote control.

Why?
====
I was looking for a simple no-fuss command line media renderer for a project I was working on for multi-room audio running on cheap storage-free computers (i.e. Raspberry Pi machines) but couldn't find anything that really met my needs.  There seems to be many, many options for running a 
media *server*, but not many options for a headless audio player.

I could have used Squeezebox/Squeeze player but they apparently do not support UPnP which was a big letdown as my NAS only does UPnP and I didn't feel like hacking its firmware to support it.  Also there is a large amount of UPnP compatible software, and I can even use the "Play to" feature from Windows 7 with UPnP.

I could have just deployed something like XMBC to the remote devices but I wanted something as small and as light as possible that just ran as an application on the machine without installing a dedicated XMBC image.

Sources
=======

This app uses one key component:
- Cling UPnP framework (LGPL) - http://4thline.org/projects/cling/

The core of the UPnP code was based loosely on the Cling examples.

It also uses the Apache HTTP Core and Client libraries.  Yes yes - those are jar files checked in.  Yes I should use maven (or similar); one day I will make that happen but for pragmatic reasons for now I am just checking in everything needed to run.

Trademarks
==========
Although this application refers to and is primarily aimed at the Raspberry Pi computer, we're nothing to do with them.  Raspberry Pi is used here in the context of indicating compatibility between our software and the Raspberry Pi hardware - please see more here: http://www.raspberrypi.org/trademark-rules

License
=======
Copyright (C) 2012 http://www.github.com/matt1

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
