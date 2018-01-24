<?php

namespace PhpStormPlugin\toto\tata;

use \atoum;

class TestCustomNamespaceMethodCall extends atoum
{
    public function __construct()
    {
        $this->setTestNamespace('\toto\tata');
    }
}
