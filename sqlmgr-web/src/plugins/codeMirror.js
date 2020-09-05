import CodeMirror from 'codemirror'
import "codemirror/lib/codemirror.css"
import "codemirror/theme/ayu-dark.css"
import "codemirror/addon/hint/show-hint.css"
require("codemirror/mode/sql/sql");
require("codemirror/addon/hint/show-hint");
require("codemirror/addon/hint/sql-hint");
require("codemirror/addon/selection/active-line");
require("codemirror/addon/edit/matchbrackets");
export default CodeMirror