Patched version to work in Android 7+ to circunvent the new protections in place (https://developer.android.com/about/versions/nougat/android-7.0-changes#permfilesys) that prevented that all information appeared in the web interface.

Requirements:

* setenforce 0 (Disable SELinux)
* adb shell "su -c 'mkdir /data/local/tmp/Inspeckage'"


License
-------

Copyright 2016 ac-pm

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
