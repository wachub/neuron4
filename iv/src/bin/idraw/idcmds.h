/*
 * Copyright (c) 1990, 1991 Stanford University
 *
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation, and that the name of Stanford not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission.  Stanford makes no representations about
 * the suitability of this software for any purpose.  It is provided "as is"
 * without express or implied warranty.
 *
 * STANFORD DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS.
 * IN NO EVENT SHALL STANFORD BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

/*
 * Idraw-specific commands.
 */

#ifndef idcmds_h
#define idcmds_h

#include <Unidraw/Commands/catcmds.h>
#include <Unidraw/Commands/viewcmds.h>

class MoveDialog;
class RotateDialog;
class ScaleDialog;

class OpenCmd : public ViewCompCmd {
public:
    OpenCmd(ControlInfo*, FileChooser* = nil);
    OpenCmd(Editor* = nil, FileChooser* = nil);

    virtual void Execute();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
};

class PreciseMoveCmd : public Command {
public:
    PreciseMoveCmd(ControlInfo*);
    PreciseMoveCmd(Editor* = nil);
    virtual ~PreciseMoveCmd();

    virtual void Execute();
    virtual bool Reversible();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
private:
    MoveDialog* _dialog;
};

class PreciseScaleCmd : public Command {
public:
    PreciseScaleCmd(ControlInfo*);
    PreciseScaleCmd(Editor* = nil);
    virtual ~PreciseScaleCmd();

    virtual void Execute();
    virtual bool Reversible();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
private:
    ScaleDialog* _dialog;
};

class PreciseRotateCmd : public Command {
public:
    PreciseRotateCmd(ControlInfo*);
    PreciseRotateCmd(Editor* = nil);
    virtual ~PreciseRotateCmd();

    virtual void Execute();
    virtual bool Reversible();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
private:
    RotateDialog* _dialog;
};

class NewViewCmd : public Command {
public:
    NewViewCmd(ControlInfo*);
    NewViewCmd(Editor* = nil);

    virtual void Execute();
    virtual bool Reversible();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
};

class ArrowCmd : public Command {
public:
    ArrowCmd(ControlInfo*, bool head = false, bool tail = false);
    ArrowCmd(Editor* = nil, bool head = false, bool tail = false);

    virtual void Execute();
    bool Head();
    bool Tail();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
protected:
    bool _head : 16;
    bool _tail : 16;
};
    
inline bool ArrowCmd::Head () { return _head; }
inline bool ArrowCmd::Tail () { return _tail; }

class AboutCmd : public Command {
public:
    AboutCmd(ControlInfo*);
    AboutCmd(Editor* = nil);

    virtual void Execute();
    virtual bool Reversible();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
};

class IGridSpacingCmd : public GridSpacingCmd {
public:
    IGridSpacingCmd(ControlInfo*);
    IGridSpacingCmd(Editor* = nil);

    virtual void Execute();

    virtual Command* Copy();
    virtual ClassId GetClassId();
    virtual bool IsA(ClassId);
};

#endif
