#! /usr/bin/perl
# 
# This crude hack is to sanitise the results of what the user may have
# "done" while editing the commit log message.. :-)   Peter Wemm.
#
# To use this, make it executable, and set your editinfo DEFAULT line:
# DEFAULT   /path/to/this/program
#

# same rules as CVS
$editor="/usr/bin/vi";
if (defined $ENV{'EDITOR'}) {		# $EDITOR overrides default
    $editor = $ENV{'EDITOR'};
}
if (defined $ENV{'CVSEDITOR'}) {        # $CVSEDITOR overrides $EDITOR
    $editor = $ENV{'CVSEDITOR'};
}

if (! @ARGV) {
    die("Usage: cvsedit filename\n");
}
$filename = $ARGV[0];
$tmpfile = $filename . "tmp";

system("$editor $filename");

open(IN, "< $filename")
    || die("cvsedit: Cannot open for reading: $filename: $!\n");

open(OUT, "> $tmpfile")
    || die("cvsedit: Cannot open for writing: $tmpfile: $!\n");

#
# In-place edit the result of the user's edit on the file.
#
$blank = 0;	# true if the last line was blank
$first = 0;	# true if we have seen the first real text
while (<IN>) {

    #
    # Dont let CVS: lines upset things, but maintain them in case
    # the editor is re-entered.  NO CHANGES!!
    if (/^CVS:/) {
        print OUT;
        next;
    }

    chomp;                      # strip trailing newline
    s/[\s]+$//;                 # strip trailing whitespace

    # collapse multiple blank lines, and trailing blank lines.
    if (/^$/) {
        # Blank line. Remember in case more text follows.
        $blank = 1;
        next;
    }
    else {
        # Delete if they only have whitespace after them.
        if (/^Reviewed by:$/i ||
            /^Submitted by:$/i ||
            /^Obtained from:$/i ||
            /^CC:$/i) {
            next;
        }
        if ($blank && $first) {
            # Previous line(s) was blank, this isn't. Close the
            # collapsed section.
            print OUT "\n";
        }
        $blank = 0;	# record non-blank
        $first = 1;	# record first line
        print OUT "$_\n";
    }
}
close(IN);
close(OUT);

unlink($filename . "~");	# Nuke likely editor backups..
unlink($filename . ".bak");	# Nuke likely editor backups..

# Check to see if any differences.
$different = system("cmp -s $tmpfile $filename");

# Make a clean exit if there are no changes, or there is no 'text'
# - for example, a user quit the editor without saving.
# preserve stat() info if appropriate.
if ((! $different) || (! $first)) {
    unlink($tmpfile);
    exit(0);
}

rename("$tmpfile", "$filename")
    || die("cvsedit: Could not rename $tmpfile to $filename: $!");

exit(0);
