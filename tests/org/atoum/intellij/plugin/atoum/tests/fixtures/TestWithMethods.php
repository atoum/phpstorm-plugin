<?php

namespace PhpStormPlugin\tests\units;

use atoum;

/**
 * /!\ MODIFY THIS FILE WILL BREAK TEST IN RunTest.java BECAUSE IT RELY ON CARET POSITION IN THE FILE
 */

class TestWithMethods extends atoum
{
    public function beforeTestMethod($method)
    {
        if (defined('PHP_WINDOWS_VERSION_MAJOR') === false) {
            $this->skip('Can only run on Windows');
        }
    }

    public function test__construct_bad()
    {
        $this->assert
            ->exception(function () {
                new \Pickle\Engine\PHP("");
            });

        $this->assert
            ->exception(function () {
                new \Pickle\Engine\PHP("c:\\windows\\system32\\at.exe");
            });
    }

    public function test__construct_ok()
    {
        $p = new \Pickle\Engine\PHP();

        $this
            ->object($p)
                ->isInstanceOf('\Pickle\Engine\PHP');
    }
}
