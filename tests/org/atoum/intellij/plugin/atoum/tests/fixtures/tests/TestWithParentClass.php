<?php

namespace PhpStormPlugin\tests\units;

abstract class DatabaseTest extends \atoum
{

}

abstract class ApiTest extends DatabaseTest
{

}

class TestWithParentClass extends ApiTest
{

}
