#! /bin/sh

#
# capture log
#
TEMPFILE=/tmp/cvslog$$.tmp
cat > $TEMPFILE

#
# Invoke spackle
#
$CVSROOT/CVSROOT/log_accum.pl $USER "`echo ${1} | cut -d , -f 1`" < $TEMPFILE

#
# Invoke cvsspam
#
$CVSROOT/CVSROOT/collect_diffs.rb --from $USER --to coral-cvshtml@lists.caltha.pl "${1}" < $TEMPFILE

#
# clean up
#
rm $TEMPFILE