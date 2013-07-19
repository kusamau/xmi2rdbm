xquery version "1.0";
(:~~~~~~~~~~~~~~~~~~~~~~~~~~ Conformance Test  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Test Number:	001
Description:	Asserts the existence of at least one outermost UML:Package with the 
				stereotype <<Application Schema>>.
Reference:		
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~:)

declare namespace UML	= "omg.org/UML1.3";
declare namespace cr	= "http://ndg.service.newmoon.conftest-result/1.0";

(: Declare local variables, particular to this test :)
declare variable $test-num as xs:integer := 1;
declare variable $pass-msg as xs:string := "A package with the stereotype &lt;&lt;Application Schema&gt;&gt; is present.";
declare variable $fail-msg as xs:string := "A package with the stereotype &lt;&lt;Application Schema&gt;&gt; is NOT present.";

(: Declare the local assert function which defines a postive condition for pass :)
declare function local:assert($doc-root as node()?) as node()?
{
	if (exists(//UML:Package[UML:ModelElement.taggedValue/UML:TaggedValue/@tag = "stereotype" and UML:ModelElement.taggedValue/UML:TaggedValue/@value = ("Application Schema", "applicationSchema") and empty(ancestor::UML:Package)])) then ()
	else <cr:fail />
};

(: Create a new result element :)
mod-fr:new-result($test-num, $pass-msg, $fail-msg, local:assert(/))
