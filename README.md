# atoum PhpStorm Plugin

Integrates atoum into PhpStorm.

Features :

* Go to the test class from the tested class (shortcut : alt+shift+K)
* Go to the tested class from the test class (shortcut : alt+shift+K)
* Execute tests inside PhpStorm (shortcut : alt+shift+M)
* Easily identify test files by a custom icon


## Detailed Features


### Execute tests inside PhpStorm

By an entry on the run menu and on the right click menu.

![Demo](doc/run.png)

You can run the tests from both the test file or the tested classe's file.

Your test file and tested file will automatically be save before running the test.

The default keyboard shortcut to execute the test is alt+shift+M.


### Go to the test class from the tested class

From the tested class you can go to the test class by clicking on the icon on the left of the class,

![Demo](doc/switch-icon.png)

or from a menu entry in navigation,

![Demo](doc/switch.png)

or by an entry bin the right click menu.

![Demo](doc/switch-right_click.png)

The default keyboard shortcut to switch file is alt+shift+K.


### Go to the tested class from the test class

From the test class you can go to the tested class by clicking on the icon on the left of the class,

![Demo](doc/switch_back-icon.png)

or from a menu entry in navigation,

![Demo](doc/switch_back.png)

or by an entry bin the right click menu.

![Demo](doc/switch_back-right_click.png)

The default keyboard shortcut to switch file is alt+shift+K.


### Easily identify test files by a custom icon

Atoum's test files are displayed with a different icon, like that you will easily differentiate them from other PHP files.

![Demo](doc/custom_icon-tabs.png)

![Demo](doc/custom_icon-tree.png)


## Installation

### Inside PhpStorm (recommended)

* Open PhpStorm
* Go to `File -> Settings`, then click on `Plugins`
* Click on `Browse repositories`
* Search for atoum in the list, then click on the install button
* Restart PhpStorm

### Via the jar

* Download the lasted version of the `.jar` on the [releases pages](https://github.com/atoum/phpstorm-plugin/releases)
* Open PhpStorm
* Go to `File -> Settings`, then click on `Plugins`
* Click on the `install from disk` button, then select to downloaded jar
* Restart PhpStorm


## Links

* [PhpStorm](https://www.jetbrains.com/phpstorm/)
* [atoum](http://atoum.org)
* [atoum's documentation](http://docs.atoum.org)


## Licence

atoum phpstorm-plugin is released under the MIT License. See the bundled LICENSE file for details.
