BASEDIR=/home/roman/Documents/workspace-sts-3.0.0.M3/sokrathes1
XSL_FILE=~/opt/docbook-xsl-1.77.1/html/docbook.xsl
#XSL_FILE=~/opt/docbook-xsl-1.77.1/xhtml5/docbook.xsl
if [ -d $BASEDIR/target/site/docbook ]; then
	echo "Folder exist"
else
	mkdir $BASEDIR/target/site/docbook
fi
xsltproc $XSL_FILE $BASEDIR/src/site/docbook/$1.xml > $BASEDIR/target/site/docbook/$1.html
mv docbook.css $BASEDIR/target/site/docbook/
cp -r $BASEDIR/src/site/docbook/img $BASEDIR/target/site/docbook/
#ls -l $BASEDIR/target/site/docbook/

