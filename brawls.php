<?php
echo "<html><body><table>\n\n";
echo "<tr><td>nick</td><td>score</td></tr>\n";
$f = fopen("../brawls.csv", "r");
$i = 0;
while (($line = fgetcsv($f)) !== false)
{
    echo "<tr>";
    foreach ($line as $cell)
    {
	    $i++;
        echo "<td>" . htmlspecialchars($cell) . "</td>";
		if (($i % 2) == 0)
		{
		    echo "</tr>\n<tr>";
		}
    }
    echo "</tr>\n";
}
fclose($f);
echo "\n</table></body></html>";