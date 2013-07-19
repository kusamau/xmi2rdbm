xquery version "1.0";
(:~~~~~~~~~~~~~~~~~~~~~~~~~~ Conformance Test  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Test Number:	002
Description:	Asserts the existence of exactly one outermost UML:Package
				with the stereotype <<Application Schema>>. In following
				tests refered to as the "target Application Schema package".
Reference:		
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~:)

import module namespace mod-fr = "urn:local-module:ISO19136-V3.2-AnxE_XMI-V1.1:framework"
at "xmldb:exist:///db/modules/conformance-test/ISO19136-V3.2-AnxE_XMI-V1.1/module-framework-functions.xq";

declare namespace UML	= "omg.org/UML1.3";
declare namespace cr	= "http://ndg.service.newmoon.conftest-result/1.0";

(: Declare local variables, particular to this test :)
declare variable $test-num as xs:integer := 2;
declare variable $pass-msg as xs:string := "The root (outermost) package does have the stereotype &lt;&lt;Application Schema&gt;&gt; AND no other package has the stereotype &lt;&lt;Application Schema&gt;&gt;.";
declare variable $fail-msg as xs:string := "The model is not serialized properly because:";

(:
	#Date:		2008-11-28
	#Author:	Pavel Golodoniuc

	Declare the local assert function which defines a postive condition for pass.
:)
declare function local:assert($doc-root as node()?) as node()?
{
	let
		$packages := $doc-root//UML:Package,
		$fail-root := (
			if ($packages[1]/UML:ModelElement.taggedValue/UML:TaggedValue[@tag eq "stereotype" and @value = ("Application Schema", "applicationSchema")]) then ()
			else
				<cr:message>{ concat("The root (outermost) package ", $packages[1]/@name, " does NOT have the stereotype &lt;&lt;Application Schema&gt;&gt;.") }</cr:message>
		),
		$fail-nested := (
			for $pck in $packages[position() gt 1]
			return
				if ($pck[1]/UML:ModelElement.taggedValue/UML:TaggedValue[@tag eq "stereotype"]/@value != ("Application Schema", "applicationSchema")) then ()
				else
					<cr:message>{ concat("The package ", $pck[1]/@name, " has the stereotype &lt;&lt;Application Schema&gt;&gt; but it is not the root (outermost) package.") }</cr:message>
		)
	return
		if (empty($fail-root) and empty($fail-nested)) then ()
		else
			<cr:fail>
				<cr:messages>{ $fail-root, $fail-nested }</cr:messages>
			</cr:fail>
};

(: Create a new result element :)
mod-fr:new-result($test-num, $pass-msg, $fail-msg, local:assert(/))
