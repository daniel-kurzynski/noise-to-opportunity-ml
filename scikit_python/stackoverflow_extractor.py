# from xml.etree import cElementTree as ET
import lxml.etree as ET, codecs, re
from os.path import *

FOLDER = "D:/SMM_DATA"
SMALL_FILE_PATH = join(FOLDER, "Posts_small.xml")
# FILE_PATH = join(FOLDER, "Posts.xml")
"""
	<row
		Id="4"
		PostTypeId="1"
		AcceptedAnswerId="7"
		CreationDate="2008-07-31T21:42:52.667"
		Score="305"
		ViewCount="20324"
		Body="&lt;p&gt;I want to use a track-bar to change a form's opacity.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;This is my code:&lt;/p&gt;&#xA;&#xA;&lt;pre&gt;&lt;code&gt;decimal trans = trackBar1.Value / 5000;&#xA;this.Opacity = trans;&#xA;&lt;/code&gt;&lt;/pre&gt;&#xA;&#xA;&lt;p&gt;When I try to build it, I get this error:&lt;/p&gt;&#xA;&#xA;&lt;blockquote&gt;&#xA;  &lt;p&gt;Cannot implicitly convert type 'decimal' to 'double'.&lt;/p&gt;&#xA;&lt;/blockquote&gt;&#xA;&#xA;&lt;p&gt;I tried making &lt;code&gt;trans&lt;/code&gt; a &lt;code&gt;double&lt;/code&gt;, but then the control doesn't work. This code has worked fine for me in VB.NET in the past. &lt;/p&gt;&#xA;"
		OwnerUserId="8"
		LastEditorUserId="451518"
		LastEditorDisplayName="Rich B"
		LastEditDate="2014-07-28T10:02:50.557"
		LastActivityDate="2014-07-28T10:02:50.557"
		Title="When setting a form's opacity should I use a decimal or double?"
		Tags="&lt;c#&gt;&lt;winforms&gt;&lt;type-conversion&gt;&lt;opacity&gt;"
		AnswerCount="13" CommentCount="1" FavoriteCount="28" CommunityOwnedDate="2012-10-31T16:42:47.213" />
"""


rows = map(
	lambda row: (
		bool(row.get("AcceptedAnswerId")),
		row.get("Title", "") + " " + row.get("Body", "")
		),
	ET.parse(SMALL_FILE_PATH).getroot().iter("row"))

def clean(content):
	content = content.replace("\n", " ")
	# TODO: remove <code> blocks
	return re.sub(r'<[\/=\.\:\w\s\*\#\"\'\(\)]*>\n{0,1}', "", content)


questions, answers = map(lambda f_name: codecs.open(join(FOLDER, f_name), "w", "utf-8"), ["demand.txt", "no-demand.txt"])

for row in rows:
	if row[0]:
		questions.write(clean(row[1]) + u"\n")
	else:
		answers.write(clean(row[1]) + u"\n")

questions.close()
answers.close()
